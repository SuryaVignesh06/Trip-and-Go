"use client";

import { FormEvent, useEffect, useState } from "react";
import { UserShell } from "../components/user-shell";
import { api, Destination, UserProfile } from "../lib/api";

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [preferences, setPreferences] = useState<
    Record<string, string>
  >({});

  const [favorites, setFavorites] = useState<Destination[]>([]);
  const [preferenceStatus, setPreferenceStatus] = useState("");

  useEffect(() => {
    api<UserProfile>("/users/me")
      .then((data) => {
        setProfile(data);
        setName(data.name || "");
        setLocation(data.location || "");
      })
      .catch((reason) =>
        setError(
          reason instanceof Error
            ? reason.message
            : "Profile could not be loaded."
        )
      );

    api<Record<string, string>>("/users/me/preferences")
      .then((data) => setPreferences(data || {}))
      .catch(() => setPreferences({}));

    api<Destination[]>("/users/me/favorites")
      .then((data) => setFavorites(data || []))
      .catch(() => setFavorites([]));
  }, []);

  async function save(event: FormEvent) {
    event.preventDefault();
    setStatus("");

    try {
      const updated = await api<UserProfile>("/users/me", {
        method: "PUT",
        body: JSON.stringify({
          name,
          location,
        }),
      });

      setProfile(updated);
      setStatus("Profile updated");
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "Profile could not be updated."
      );
    }
  }

  async function savePreferences() {
    setPreferenceStatus("");

    try {
      await api("/users/me/preferences", {
        method: "PUT",
        body: JSON.stringify(preferences),
      });

      setPreferenceStatus("Travel preferences updated");
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "Preferences could not be updated."
      );
    }
  }

  async function removeFavorite(id: string | number) {
    try {
      await api(`/users/me/favorites/${id}`, {
        method: "DELETE",
      });

      setFavorites((current) =>
        current.filter(
          (destination) => destination.id !== id
        )
      );
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "Favourite destination could not be removed."
      );
    }
  }

  return (
    <UserShell
      title="Profile"
      description="Manage your personal travel identity and travel snapshot."
      activePage="Profile"
    >
      <div className="mb-5 space-y-3">
        {error && (
          <p className="rounded-2xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            {error}
          </p>
        )}

        {status && (
          <p className="rounded-2xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
            {status}
          </p>
        )}
      </div>

      {profile ? (
        <form
          onSubmit={save}
          className="grid gap-6 lg:grid-cols-[0.82fr_1.18fr]"
        >
          {/* Profile Information */}
          <section className="rounded-[28px] border border-[#dfe9ff] bg-[#f7f9ff] p-5 shadow-sm">
            <div className="flex items-center gap-4">
              <div className="flex h-20 w-20 items-center justify-center rounded-full bg-[linear-gradient(135deg,_#0d4ad5_0%,_#123ca0_100%)] text-2xl font-bold text-white">
                {profile.name.charAt(0).toUpperCase()}
              </div>

              <div>
                <h3 className="text-xl font-semibold text-[#1d2f45]">
                  {profile.name}
                </h3>

                <p className="text-sm text-[#64799a]">
                  {profile.email}
                </p>
              </div>
            </div>

            <div className="mt-6 space-y-3 text-sm text-[#53667e]">
              <div className="rounded-2xl bg-white p-3">
                <strong className="text-[#1d2f45]">
                  Email:
                </strong>{" "}
                {profile.email}
              </div>

              <div className="rounded-2xl bg-white p-3">
                <strong className="text-[#1d2f45]">
                  Location:
                </strong>{" "}
                {profile.location || "Not provided"}
              </div>
            </div>
          </section>

          {/* Edit Personal Details */}
          <section className="rounded-[28px] border border-[#dfe9ff] bg-[#f7f9ff] p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-[#1d2f45]">
              Edit personal details
            </h3>

            <label className="mt-5 block text-sm font-medium text-[#53667e]">
              Full name

              <input
                value={name}
                onChange={(event) =>
                  setName(event.target.value)
                }
                className="mt-2 w-full rounded-2xl border border-[#dfe9ff] bg-white px-3 py-3 text-[#1d2f45] outline-none transition focus:border-[#7ea5ff] focus:ring-4 focus:ring-[#dfeaff]"
              />
            </label>

            <label className="mt-4 block text-sm font-medium text-[#53667e]">
              Location

              <input
                value={location}
                onChange={(event) =>
                  setLocation(event.target.value)
                }
                className="mt-2 w-full rounded-2xl border border-[#dfe9ff] bg-white px-3 py-3 text-[#1d2f45] outline-none transition focus:border-[#7ea5ff] focus:ring-4 focus:ring-[#dfeaff]"
              />
            </label>

            <button
              type="submit"
              className="mt-5 rounded-full bg-[#0d4ad5] px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-[#dfeaff] transition hover:bg-[#0b3eb1]"
            >
              Save changes
            </button>
          </section>

          {/* Travel Preferences */}
          <section className="rounded-[28px] border border-[#dfe9ff] bg-[#f7f9ff] p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-[#1d2f45]">
              Travel Preferences
            </h3>

            <div className="mt-5 space-y-4">
              {[
                "preferredDestinations",
                "travelType",
                "climate",
                "accommodation",
                "activities",
                "budgetRange",
              ].map((field) => (
                <label
                  key={field}
                  className="block text-sm font-medium text-[#53667e]"
                >
                  {field
                    .replace(/([A-Z])/g, " $1")
                    .replace(/^./, (letter) =>
                      letter.toUpperCase()
                    )}

                  <input
                    value={preferences[field] || ""}
                    onChange={(event) =>
                      setPreferences((current) => ({
                        ...current,
                        [field]: event.target.value,
                      }))
                    }
                    className="mt-2 w-full rounded-2xl border border-[#dfe9ff] bg-white px-3 py-3 text-[#1d2f45] outline-none transition focus:border-[#7ea5ff] focus:ring-4 focus:ring-[#dfeaff]"
                  />
                </label>
              ))}
            </div>

            {preferenceStatus && (
              <p className="mt-4 text-sm text-emerald-700">
                {preferenceStatus}
              </p>
            )}

            <button
              type="button"
              onClick={savePreferences}
              className="mt-5 rounded-full bg-[#0d4ad5] px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-[#dfeaff] transition hover:bg-[#0b3eb1]"
            >
              Save travel preferences
            </button>
          </section>

          {/* Favourite Destinations */}
          <section className="rounded-[28px] border border-[#dfe9ff] bg-[#f7f9ff] p-5 shadow-sm">
            <h3 className="text-lg font-semibold text-[#1d2f45]">
              Favourite Destinations
            </h3>

            {favorites.length > 0 ? (
              <div className="mt-5 grid gap-3">
                {favorites.map((destination) => (
                  <div
                    key={destination.id}
                    className="flex items-center justify-between rounded-2xl bg-white p-4"
                  >
                    <div>
                      <h4 className="font-semibold text-[#1d2f45]">
                        {destination.name}
                      </h4>

                      {destination.country && (
                        <p className="mt-1 text-sm text-[#64799a]">
                          {destination.country}
                        </p>
                      )}
                    </div>

                    <button
                      type="button"
                      onClick={() =>
                        removeFavorite(destination.id)
                      }
                      className="rounded-full border border-red-200 px-3 py-1.5 text-sm font-medium text-red-600 transition hover:bg-red-50"
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="mt-5 rounded-2xl bg-white p-4 text-sm text-[#64799a]">
                No favourite destinations yet.
              </p>
            )}
          </section>
        </form>
      ) : (
        <p className="text-sm text-[#64799a]">
          Loading profile...
        </p>
      )}
    </UserShell>
  );
}