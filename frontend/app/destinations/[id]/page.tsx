"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { UserShell } from "../../components/user-shell";
import { api, Destination } from "../../lib/api";

type WeatherResponse = { temperature: number; condition: string; source: string };

export default function DestinationDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const [destination, setDestination] = useState<Destination | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadDestination() {
    setLoading(true);
    setError("");
    try {
      const destinationData = await api<Destination>(`/destinations/${encodeURIComponent(id)}`);
      setDestination(destinationData);
      try {
        const weather = await api<WeatherResponse>(`/weather?city=${encodeURIComponent(destinationData.name)}&country=${encodeURIComponent(destinationData.country || "")}`);
        setDestination({ ...destinationData, weather: { temperature: `${Math.round(weather.temperature)}°C`, condition: weather.condition, source: weather.source } });
      } catch {
        // Keep destination details available when the provider is unavailable or not configured.
      }
    } catch (reason) {
      setDestination(null);
      setError(reason instanceof Error ? reason.message : "Destination details could not be loaded.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!id) return;
    const timer = window.setTimeout(() => void loadDestination(), 0);
    return () => window.clearTimeout(timer);
    // Data should refresh only when the route parameter changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  return (
    <UserShell title={destination?.name || "Destination details"} description={destination ? `${destination.name}, ${destination.country}` : "Explore destination details."} activePage="Destinations">
      <Link href="/destinations" className="text-sm font-semibold text-[#396c68]">← Back to destinations</Link>

      {loading && <div className="mt-6 grid gap-5 md:grid-cols-[1.3fr_0.7fr]"><div className="h-72 animate-pulse rounded-2xl bg-slate-100" /><div className="h-72 animate-pulse rounded-2xl bg-slate-100" /></div>}

      {!loading && error && <div className="mt-6 rounded-2xl border border-red-100 bg-red-50 p-5 text-red-700"><p className="font-semibold">Destination unavailable</p><p className="mt-2 text-sm">{error}</p><button type="button" onClick={() => void loadDestination()} className="mt-4 rounded-lg bg-white px-3 py-2 text-sm font-semibold">Try again</button></div>}

      {!loading && destination && <div className="mt-6 grid gap-5 md:grid-cols-[1.25fr_0.75fr]">
        <section>
          <div className="rounded-2xl bg-[#edf5f4] p-6"><p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#5d7a78]">About the destination</p><p className="mt-4 text-lg leading-8 text-slate-700">{destination.description || "A destination worth discovering. Explore local highlights and start shaping your trip."}</p></div>
          <div className="mt-6"><h3 className="text-xl font-semibold text-slate-900">Destination highlights</h3>{destination.highlights?.length ? <ul className="mt-3 grid gap-2 sm:grid-cols-2">{destination.highlights.map((highlight) => <li key={highlight} className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-600">{highlight}</li>)}</ul> : <p className="mt-3 rounded-xl bg-slate-50 p-4 text-sm text-slate-500">More local details will appear here as they become available.</p>}</div>
        </section>
        <aside className="h-fit rounded-2xl bg-[#173f45] p-6 text-white"><div className="flex items-start justify-between gap-4"><div><p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#9dd6ca]">Live weather</p><p className="mt-1 text-xs text-slate-300">Current conditions</p></div><span className="text-2xl" aria-hidden="true">☼</span></div>{destination.weather ? <><p className="mt-8 text-5xl font-semibold">{destination.weather.temperature}</p><p className="mt-2 text-slate-200">{destination.weather.condition}</p><p className="mt-8 border-t border-white/15 pt-3 text-xs text-slate-300">Source: {destination.weather.source}</p></> : <p className="mt-8 text-sm text-slate-300">Weather information is currently unavailable.</p>}</aside>
      </div>}
    </UserShell>
  );
}
