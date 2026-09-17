"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { UserShell } from "../../components/user-shell";
import { getWeather, WeatherResponse } from "../../lib/api";

export default function DestinationDetailsPage() {
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [latitude, setLatitude] = useState<number | null>(null);
  const [longitude, setLongitude] = useState<number | null>(null);

  const [weather, setWeather] = useState<WeatherResponse | null>(null);
  const [loadingWeather, setLoadingWeather] = useState(true);
  const [weatherError, setWeatherError] = useState("");

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    const placeName =
      params.get("name") || "Destination";

    const placeAddress =
      params.get("address") || "";

    const lat = Number(params.get("lat"));
    const lon = Number(params.get("lon"));

    setName(placeName);
    setAddress(placeAddress);

    if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
      setLoadingWeather(false);
      setWeatherError(
        "Destination coordinates are missing or invalid."
      );
      return;
    }

    setLatitude(lat);
    setLongitude(lon);

    async function loadWeather() {
      setLoadingWeather(true);
      setWeatherError("");

      try {
        const result = await getWeather(lat, lon);
        setWeather(result);
      } catch (reason) {
        setWeatherError(
          reason instanceof Error
            ? reason.message
            : "Weather information could not be loaded."
        );
      } finally {
        setLoadingWeather(false);
      }
    }

    void loadWeather();
  }, []);

  const temperature =
    weather?.main?.temp !== undefined
      ? `${Math.round(weather.main.temp)}°C`
      : "--";

  const condition =
    weather?.weather?.[0]?.description ||
    "Weather unavailable";

  return (
    <UserShell
      title="Destination Details"
      description="Explore the location before building your trip."
      activePage="Destinations"
    >
      <div className="mb-5">
        <Link
          href="/destinations"
          className="text-sm font-semibold text-[#0d4ad5]"
        >
          ← Back to destinations
        </Link>
      </div>

      <section className="rounded-[28px] bg-[linear-gradient(135deg,_#0d4ad5_0%,_#123ca0_100%)] p-6 text-white shadow-[0_20px_40px_rgba(13,74,213,0.2)]">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-blue-100/80">
          Destination
        </p>

        <h2 className="mt-2 text-3xl font-semibold">
          {name}
        </h2>

        {address && (
          <p className="mt-2 max-w-3xl text-sm leading-6 text-blue-100">
            {address}
          </p>
        )}
      </section>

      <div className="mt-6 grid gap-5 lg:grid-cols-2">
        {/* Location */}
        <section className="rounded-[26px] border border-[#dfe9ff] bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#6d82a3]">
            Location
          </p>

          <h3 className="mt-2 text-xl font-semibold text-[#1d2f45]">
            Coordinates
          </h3>

          <div className="mt-5 grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl bg-[#f7f9ff] p-4">
              <p className="text-xs font-semibold uppercase text-[#71839d]">
                Latitude
              </p>

              <p className="mt-1 text-lg font-semibold text-[#1d2f45]">
                {latitude ?? "--"}
              </p>
            </div>

            <div className="rounded-2xl bg-[#f7f9ff] p-4">
              <p className="text-xs font-semibold uppercase text-[#71839d]">
                Longitude
              </p>

              <p className="mt-1 text-lg font-semibold text-[#1d2f45]">
                {longitude ?? "--"}
              </p>
            </div>
          </div>

          <p className="mt-5 text-sm leading-6 text-[#64799a]">
            These coordinates came from the OpenStreetMap Nominatim
            search result.
          </p>
        </section>

        {/* Weather */}
        <section className="rounded-[26px] border border-[#dfe9ff] bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#6d82a3]">
            Current weather
          </p>

          <h3 className="mt-2 text-xl font-semibold text-[#1d2f45]">
            Weather information
          </h3>

          {loadingWeather ? (
            <div className="mt-5 rounded-2xl bg-[#f7f9ff] p-6">
              <div className="h-8 w-24 animate-pulse rounded bg-[#e7efff]" />

              <div className="mt-3 h-4 w-40 animate-pulse rounded bg-[#e7efff]" />
            </div>
          ) : weatherError ? (
            <div className="mt-5 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              <p className="font-semibold">
                Weather could not be loaded.
              </p>

              <p className="mt-1">
                {weatherError}
              </p>
            </div>
          ) : weather ? (
            <div className="mt-5">
              <p className="text-4xl font-semibold text-[#1d2f45]">
                {temperature}
              </p>

              <p className="mt-2 text-base capitalize text-[#53667e]">
                {condition}
              </p>

              <div className="mt-5 grid gap-3 sm:grid-cols-2">
                <div className="rounded-2xl bg-[#f7f9ff] p-4">
                  <p className="text-xs font-semibold uppercase text-[#71839d]">
                    Feels like
                  </p>

                  <p className="mt-1 font-semibold text-[#1d2f45]">
                    {Math.round(weather.main.feels_like)}°C
                  </p>
                </div>

                <div className="rounded-2xl bg-[#f7f9ff] p-4">
                  <p className="text-xs font-semibold uppercase text-[#71839d]">
                    Humidity
                  </p>

                  <p className="mt-1 font-semibold text-[#1d2f45]">
                    {weather.main.humidity}%
                  </p>
                </div>

                <div className="rounded-2xl bg-[#f7f9ff] p-4">
                  <p className="text-xs font-semibold uppercase text-[#71839d]">
                    Wind
                  </p>

                  <p className="mt-1 font-semibold text-[#1d2f45]">
                    {weather.wind.speed} m/s
                  </p>
                </div>

                <div className="rounded-2xl bg-[#f7f9ff] p-4">
                  <p className="text-xs font-semibold uppercase text-[#71839d]">
                    Pressure
                  </p>

                  <p className="mt-1 font-semibold text-[#1d2f45]">
                    {weather.main.pressure} hPa
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <p className="mt-5 text-sm text-[#64799a]">
              No weather information is available.
            </p>
          )}
        </section>
      </div>

      <section className="mt-5 rounded-[26px] border border-[#dfe9ff] bg-[#f7f9ff] p-6">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#6d82a3]">
          Next step
        </p>

        <h3 className="mt-2 text-xl font-semibold text-[#1d2f45]">
          Ready to plan a trip?
        </h3>

        <p className="mt-2 max-w-2xl text-sm leading-6 text-[#64799a]">
          Once you choose this destination, we can connect it to
          TripNest&apos;s trip creation flow.
        </p>

        <Link
          href="/my-trips"
          className="mt-5 inline-flex rounded-full bg-[#0d4ad5] px-5 py-3 text-sm font-semibold text-white transition hover:bg-[#0b3fb7]"
        >
          Go to My Trips
        </Link>
      </section>

      <p className="mt-5 text-center text-xs text-[#71839d]">
        Location data powered by OpenStreetMap Nominatim.
      </p>
    </UserShell>
  );
}