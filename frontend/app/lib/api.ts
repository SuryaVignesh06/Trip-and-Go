export type Activity = {
  id: number;
  title: string;
  type?: string;
  startTime?: string;
  endTime?: string;
  location?: string;
  notes?: string;
};

export type Day = {
  id: number;
  dayNumber: number;
  date?: string;
  notes?: string;
  activities: Activity[];
};

export type Trip = {
  id: number;
  title: string;
  destination?: {
    id?: number;
    name?: string;
    country?: string;
    description?: string;
  } | string;
  destinationId?: number;
  startDate?: string;
  endDate?: string;
  description?: string;
  budget?: number;
  status: string;
  days?: Day[];
  owner?: {
    id?: number;
    name?: string;
    email?: string;
  };
};

export type Destination = {
  id: string | number;
  name: string;
  country?: string;
  description?: string;
  latitude?: number;
  longitude?: number;
  weatherInfo?: string;
  isPopular?: boolean;
  weather?: {
    temperature: string;
    condition: string;
    source: string;
  };
  highlights?: string[];
};

export type NominatimPlace = {
  place_id: number;
  name: string;
  display_name: string;
  lat: string;
  lon: string;
  type: string;
  address?: {
    city?: string;
    town?: string;
    village?: string;
    state?: string;
    country?: string;
    country_code?: string;
    [key: string]: string | undefined;
  };
};

export type Budget = {
  id?: number;
  amount: number | string;
  currency: string;
  trip?: {
    id?: number;
  };
};

export type Expense = {
  id?: number;
  category: string;
  amount: number | string;
  date: string;
  receiptLink?: string;
  trip?: {
    id?: number;
  };
  payer?: {
    id?: number;
    name?: string;
    email?: string;
  };
};

export type CategorySummaryItem = {
  category: string;
  total: number;
};

export type CategorySummaryResponse =
  | CategorySummaryItem
  | [string, number | string];

export type UserProfile = {
  id?: number;
  name: string;
  email: string;
  location?: string;
  languages?: string[];
  preferences?: Record<string, string | string[]>;
  favoriteDestinations?: Destination[];
};

export type TripMemberRole = "OWNER" | "GROUP_ADMIN" | "MEMBER";

export type TripMember = {
  id: number;
  name: string;
  email: string;
  role: TripMemberRole;
};

export type TripSearchResult = {
  id: number;
  title: string;
  destination?: string;
  ownerName?: string;
  memberCount?: number;
};

/* =========================
   Notification
   ========================= */

export type Notification = {
  id: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
};

const API_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

const REQUEST_TIMEOUT_MS = 15_000;

function session() {
  if (typeof window === "undefined") {
    return { token: "", email: "" };
  }

  return {
    token: localStorage.getItem("tripnest_token") || "",
    email: localStorage.getItem("tripnest_email") || "",
  };
}

export async function api<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const { token, email } = session();

  const url = path.startsWith("http")
    ? path
    : `${API_URL}${path}`;

  const hasQuery = url.includes("?");

  const withEmail =
    Boolean(email) &&
    (path.startsWith("/trips") || path.startsWith("/users"));

  const controller = new AbortController();

  const timeout = window.setTimeout(
    () => controller.abort(),
    REQUEST_TIMEOUT_MS
  );

  let response: Response;

  try {
    response = await fetch(
      `${url}${
        withEmail
          ? `${hasQuery ? "&" : "?"}email=${encodeURIComponent(email)}`
          : ""
      }`,
      {
        ...options,
        signal: options.signal || controller.signal,
        headers: {
          "Content-Type": "application/json",
          ...(token
            ? { Authorization: `Bearer ${token}` }
            : {}),
          ...options.headers,
        },
      }
    );
  } catch (error) {
    if (
      error instanceof DOMException &&
      error.name === "AbortError"
    ) {
      throw new Error(
        "The request timed out. Please check your connection and try again."
      );
    }

    throw new Error(
      "Unable to reach the service. Please try again."
    );
  } finally {
    window.clearTimeout(timeout);
  }

  const text = await response.text();

  if (!response.ok) {
    let message =
      text || `Request failed (${response.status})`;

    try {
      const payload = JSON.parse(text) as {
        message?: string;
        error?: string;
      };

      message =
        payload.message ||
        payload.error ||
        message;
    } catch {}

    throw new Error(message);
  }

  if (!text) {
    return undefined as T;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return text as unknown as T;
  }
}

export function saveSession(
  email: string,
  token: string | null
) {
  localStorage.setItem("tripnest_email", email);

  if (token) {
    localStorage.setItem("tripnest_token", token);
  } else {
    localStorage.removeItem("tripnest_token");
  }
}

export function clearSession() {
  localStorage.removeItem("tripnest_email");
  localStorage.removeItem("tripnest_token");
}

/* =========================
   Budget APIs
   ========================= */

export async function getBudget(
  tripId: number
): Promise<Budget> {
  return api(`/api/budgets/${tripId}`);
}

export async function createBudget(
  tripId: number,
  budget: Omit<Budget, "id">
): Promise<Budget> {
  return api(`/api/budgets/${tripId}`, {
    method: "POST",
    body: JSON.stringify(budget),
  });
}

export async function updateBudget(
  tripId: number,
  budget: Omit<Budget, "id">
): Promise<Budget> {
  return api(`/api/budgets/${tripId}`, {
    method: "PUT",
    body: JSON.stringify(budget),
  });
}

/* =========================
   Expense APIs
   ========================= */

export async function getExpenses(
  tripId: number
): Promise<Expense[]> {
  return api(`/api/expenses/trip/${tripId}`);
}

export async function createExpense(
  tripId: number,
  expense: Omit<Expense, "id">
): Promise<Expense> {
  return api(`/api/expenses/trip/${tripId}`, {
    method: "POST",
    body: JSON.stringify(expense),
  });
}

export async function updateExpense(
  expenseId: number,
  expense: Omit<Expense, "id">
): Promise<Expense> {
  return api(`/api/expenses/${expenseId}`, {
    method: "PUT",
    body: JSON.stringify(expense),
  });
}

export async function deleteExpense(
  expenseId: number
): Promise<void> {
  return api(`/api/expenses/${expenseId}`, {
    method: "DELETE",
  });
}

export async function getCategorySummary(
  tripId: number
): Promise<CategorySummaryResponse[]> {
  return api(
    `/api/expenses/trip/${tripId}/category-summary`
  );
}

export async function getRemainingBudget(
  tripId: number
): Promise<number> {
  return api(
    `/api/expenses/trip/${tripId}/remaining-budget`
  );
}

/* =========================
   Group Collaboration APIs
   ========================= */

export async function getTripMembers(
  tripId: number
): Promise<TripMember[]> {
  return api(`/trips/${tripId}/members`);
}

export async function inviteTripMember(
  tripId: number,
  email: string
): Promise<TripMember> {
  return api(`/trips/${tripId}/members`, {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export async function updateTripMemberRole(
  tripId: number,
  memberId: number,
  role: Exclude<TripMemberRole, "OWNER">
): Promise<TripMember> {
  return api(
    `/trips/${tripId}/members/${memberId}/role`,
    {
      method: "PATCH",
      body: JSON.stringify({ role }),
    }
  );
}

export async function removeTripMember(
  tripId: number,
  memberId: number
): Promise<void> {
  return api(
    `/trips/${tripId}/members/${memberId}`,
    {
      method: "DELETE",
    }
  );
}

export async function searchTripsByName(
  name: string
): Promise<TripSearchResult[]> {
  return api(
    `/trips/search?name=${encodeURIComponent(name)}`
  );
}

export async function requestToJoinTrip(
  tripId: number
): Promise<void> {
  return api(`/trips/${tripId}/join-requests`, {
    method: "POST",
    body: "{}",
  });
}

export async function searchPlaces(
  query: string
): Promise<NominatimPlace[]> {
  return api<NominatimPlace[]>(
    `/destinations/search?query=${encodeURIComponent(query)}`
  );
}

/* =========================
   Notification APIs
   ========================= */

export async function getNotifications(): Promise<
  Notification[]
> {
  return api<Notification[]>("/api/notifications");
}

export async function getUnreadNotificationCount(): Promise<number> {
  return api<number>(
    "/api/notifications/unread/count"
  );
}

export async function markNotificationAsRead(
  notificationId: number
): Promise<void> {
  return api(
    `/api/notifications/${notificationId}/read`,
    {
      method: "PUT",
    }
  );
}

/* =========================
   Weather APIs
   ========================= */

export type WeatherResponse = {
  coord: {
    lon: number;
    lat: number;
  };
  weather: {
    id: number;
    main: string;
    description: string;
  }[];
  main: {
    temp: number;
    feels_like: number;
    temp_min: number;
    temp_max: number;
    pressure: number;
    humidity: number;
  };
  wind: {
    speed: number;
  };
  clouds: {
    all: number;
  };
  dt: number;
  sys: {
    country: string;
    sunrise: number;
    sunset: number;
  };
  timezone: number;
  id: number;
  name: string;
};

export async function getWeather(
  latitude: number,
  longitude: number
): Promise<WeatherResponse> {
  return api<WeatherResponse>(
    `/weather?latitude=${latitude}&longitude=${longitude}`
  );
}