"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { UserShell } from "../components/user-shell";
import {
  api,
  Destination,
  NominatimPlace,
  searchPlaces,
} from "../lib/api";

export default function DestinationsPage() {
  const [query, setQuery] = useState("");
  const [destinations, setDestinations] = useState<Destination[]>([]);
  const [places, setPlaces] = useState<NominatimPlace[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [hasSearched, setHasSearched] = useState(false);

  async function loadDestinations() {
    setLoading(true);
    setError("");

    try {
      const results = await api<Destination[]>("/destinations");
      setDestinations(results);
      setPlaces([]);
    } catch (reason) {
      setDestinations([]);
      setPlaces([]);
      setError(
        reason instanceof Error
          ? reason.message
          : "Destinations could not be loaded."
      );
    } finally {
      setLoading(false);
    }
  }

  async function search(event?: FormEvent) {
    event?.preventDefault();

    const searchTerm = query.trim();

    if (!searchTerm) {
      await loadDestinations();
      setHasSearched(false);
      return;
    }

    setLoading(true);
    setError("");
    setHasSearched(true);
    setDestinations([]);

    try {
      const results = await searchPlaces(searchTerm);
      setPlaces(results);
    } catch (reason) {
      setDestinations([]);
      setPlaces([]);
      setError(
        reason instanceof Error
          ? reason.message
          : "Destinations could not be loaded."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => void loadDestinations(), 0);
    return () => window.clearTimeout(timer);
    // The initial search should only run once; subsequent searches are user initiated.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <UserShell
      title="Destinations"
      description="Find a place that matches the way you want to travel."
      activePage="Destinations"
    >
      <section className="rounded-[28px] bg-[linear-gradient(135deg,_#0d4ad5_0%,_#123ca0_100%)] p-5 text-white shadow-[0_20px_40px_rgba(13,74,213,0.2)] sm:p-6">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-blue-100/80">
          Explore the world
        </p>

        <h3 className="mt-2 max-w-xl text-2xl font-semibold">
          Search places worth building a trip around.
        </h3>

        <form
          onSubmit={search}
          className="mt-5 flex flex-col gap-2 sm:flex-row"
        >
          <label className="sr-only" htmlFor="destination-search">
            Search destinations
          </label>

          <input
            id="destination-search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search cities, countries, or regions"
            className="min-w-0 flex-1 rounded-full border border-white/15 bg-white px-4 py-3 text-slate-900 outline-none shadow-sm focus:ring-4 focus:ring-blue-200"
          />

          <button
            type="submit"
            className="rounded-full bg-white px-5 py-3 font-semibold text-[#123ca0] transition hover:bg-blue-50"
          >
            {loading && hasSearched ? "Searching..." : "Search"}
          </button>
        </form>
      </section>

      <div className="mt-8 flex items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#6d82a3]">
            {hasSearched ? "Search results" : "Popular destinations"}
          </p>

          <h3 className="mt-1 text-2xl font-semibold text-[#1d2f45]">
            {hasSearched
              ? `Places matching “${query.trim()}”`
              : "Start with a favorite"}
          </h3>
        </div>

        {hasSearched && (
          <button
            type="button"
            onClick={() => {
              setQuery("");
              void search();
            }}
            className="text-sm font-semibold text-[#0d4ad5]"
          >
            Show popular
          </button>
        )}
      </div>

      {error && (
        <div className="mt-5 flex flex-col gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 sm:flex-row sm:items-center sm:justify-between">
          <p>
            We could not reach the destination service. {error}
          </p>

          <button
            type="button"
            onClick={() => (hasSearched ? void search() : void loadDestinations())}
            className="font-semibold underline"
          >
            Try again
          </button>
        </div>
      )}

      {loading ? (
        <div
          className="mt-5 grid gap-4 md:grid-cols-2"
          aria-label="Loading destinations"
        >
          {[1, 2, 3, 4].map((card) => (
            <div
              key={card}
              className="h-44 animate-pulse rounded-[24px] bg-[#edf4ff]"
            />
          ))}
        </div>
      ) : hasSearched ? (
        places.length > 0 ? (
          <div className="mt-5 grid gap-4 md:grid-cols-2">
            
          {places.map((place) => (
  <Link
    key={place.place_id}
    href={`/destinations/details?name=${encodeURIComponent(
      place.name || place.display_name.split(",")[0]
    )}&lat=${encodeURIComponent(place.lat)}&lon=${encodeURIComponent(
      place.lon
    )}&address=${encodeURIComponent(place.display_name)}`}
    className="rounded-[26px] border border-[#dfe9ff] bg-[linear-gradient(135deg,_#ffffff_0%,_#f7f9ff_100%)] p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-[#bfd3ff] hover:shadow-md"
  >
    <div className="flex items-start justify-between gap-4">
      <div>
        <p className="text-xl font-semibold text-[#1d2f45]">
          {place.name || place.display_name.split(",")[0]}
        </p>

        <p className="mt-1 text-sm text-[#64799a]">
          {place.address?.city ||
            place.address?.town ||
            place.address?.village ||
            place.address?.state ||
            place.address?.country ||
            "Location"}
        </p>
      </div>

      <span
        className="text-lg text-[#6d82a3]"
        aria-hidden="true"
      >
        ↗
      </span>
    </div>

    <p className="mt-4 line-clamp-3 text-sm leading-6 text-[#53667e]">
      {place.display_name}
    </p>

    <p className="mt-3 text-xs text-[#6d82a3]">
      Coordinates: {place.lat}, {place.lon}
    </p>
  </Link>
))}
          </div>
        ) : (
          <div className="mt-5 rounded-[26px] border border-dashed border-[#dfe9ff] bg-[#f7f9ff] p-10 text-center">
            <p className="font-semibold text-[#1d2f45]">
              No places found
            </p>

            <p className="mt-2 text-sm text-[#64799a]">
              Try another city, country, or region.
            </p>
          </div>
        )
      ) : destinations.length > 0 ? (
        <div className="mt-5 grid gap-4 md:grid-cols-2">
          {destinations.map((destination) => (
            <Link
              key={destination.id}
              href={`/destinations/${destination.id}`}
              className="group rounded-[26px] border border-[#dfe9ff] bg-[linear-gradient(135deg,_#ffffff_0%,_#f7f9ff_100%)] p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-[#bfd3ff] hover:shadow-md"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-xl font-semibold text-[#1d2f45] group-hover:text-[#0d4ad5]">
                    {destination.name}
                  </p>

                  <p className="mt-1 text-sm text-[#64799a]">
                    {destination.country}
                  </p>
                </div>

                <span
                  className="text-lg text-[#6d82a3]"
                  aria-hidden="true"
                >
                  ↗
                </span>
              </div>

              <p className="mt-4 line-clamp-3 text-sm leading-6 text-[#53667e]">
                {destination.description ||
                  "Explore this destination and get the details you need to plan your visit."}
              </p>
            </Link>
          ))}
        </div>
      ) : (
        <div className="mt-5 rounded-[26px] border border-dashed border-[#dfe9ff] bg-[#f7f9ff] p-10 text-center">
          <p className="font-semibold text-[#1d2f45]">
            No destinations found
          </p>

          <p className="mt-2 text-sm text-[#64799a]">
            Try a broader city, country, or region search.
          </p>
        </div>
      )}

      {hasSearched && (
        <p className="mt-5 text-center text-xs text-[#71839d]">
          Search results powered by OpenStreetMap Nominatim.
        </p>
      )}
    </UserShell>
  );
}