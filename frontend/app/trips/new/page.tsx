"use client";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { api, Destination } from "../../lib/api";
import { UserShell } from "../../components/user-shell";

const templates = [
  { label: "Beach escape", title: "Maldives Getaway", description: "Sunrise swims, beach clubs, and slow evenings by the shore.", status: "PLANNED" },
  { label: "City break", title: "Paris Weekend", description: "Art museums, café hopping, and a night walk through the city lights.", status: "PLANNED" },
  { label: "Nature trip", title: "Alpine Reset", description: "Mountain trails, scenic train rides, and cozy cabins under the stars.", status: "PLANNED" },
];

export default function NewTripPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    title: "",
    destinationId: "",
    startDate: "",
    endDate: "",
    description: "",
    status: "PLANNED",
  });
  const [error, setError] = useState("");
  const [destinations, setDestinations] = useState<Destination[]>([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api<Destination[]>("/destinations")
      .then(setDestinations)
      .catch((reason) => setError(reason instanceof Error ? reason.message : "Destinations could not be loaded."));
  }, []);

  const summary = useMemo(() => {
    const days = form.startDate && form.endDate ? Math.max(1, Math.ceil((new Date(form.endDate).getTime() - new Date(form.startDate).getTime()) / 86400000) + 1) : 0;
    return {
      title: form.title || "Untitled adventure",
      destination: destinations.find((destination) => String(destination.id) === form.destinationId)?.name || "Choose a destination",
      tripLength: days ? `${days} days` : "Flexible dates",
      mode: form.status || "PLANNED",
    };
  }, [form, destinations]);

  const update = (key: string, value: string) => {
    setError("");
    setForm((current) => ({ ...current, [key]: value }));
  };

  function applyTemplate(template: (typeof templates)[number]) {
    setError("");
    setForm((current) => ({ ...current, title: template.title, description: template.description, status: template.status }));
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError("");

    if (!form.title.trim()) {
      setError("Trip name is required.");
      return;
    }

    if (!form.destinationId || Number(form.destinationId) <= 0) {
      setError("Destination ID is required.");
      return;
    }

    if (!form.startDate || !form.endDate) {
      setError("Please select both start and end dates.");
      return;
    }

    if (new Date(form.endDate) < new Date(form.startDate)) {
      setError("End date cannot be before the start date.");
      return;
    }

    setSubmitting(true);

    try {
      const trip = await api<{ id: number }>("/trips", {
        method: "POST",
        body: JSON.stringify({
          title: form.title,
          destinationId: Number(form.destinationId),
          startDate: form.startDate,
          endDate: form.endDate,
          description: form.description,
          status: form.status,
        }),
      });
      router.push(`/trips/${trip.id}`);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to save trip");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <UserShell title="Plan a trip" description="Start with the essentials, then shape a memorable itinerary around your style." activePage="My Trips">
      <div className="mb-6 grid gap-4 md:grid-cols-3">
        <div className="rounded-2xl border border-[#dfece9] bg-gradient-to-br from-[#edf7f5] to-[#fdfcfb] p-4 shadow-sm">
          <p className="text-xs uppercase tracking-[0.18em] text-[#5d7a78]">Trip snapshot</p>
          <p className="mt-3 text-2xl font-bold text-[#20363b]">{summary.title}</p>
          <p className="mt-2 text-sm text-[#526873]">{summary.destination}</p>
        </div>
        <div className="rounded-2xl border border-[#dfece9] bg-[#fffaf5] p-4 shadow-sm">
          <p className="text-xs uppercase tracking-[0.18em] text-[#7d6d58]">Duration</p>
          <p className="mt-3 text-2xl font-bold text-[#2d403e]">{summary.tripLength}</p>
          <p className="mt-2 text-sm text-[#6a6a57]">Smart planning mode</p>
        </div>
        <div className="rounded-2xl border border-[#dfece9] bg-gradient-to-br from-[#eaf2fd] to-[#fcfcff] p-4 shadow-sm">
          <p className="text-xs uppercase tracking-[0.18em] text-[#5a6d92]">Current mode</p>
          <p className="mt-3 text-2xl font-bold text-[#1f2d46]">{summary.mode}</p>
          <p className="mt-2 text-sm text-[#5b6171]">Ready for takeoff</p>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.35fr_0.65fr]">
        <section className="rounded-[28px] border border-[#e5dfd7] bg-[#fffdfb] p-5 shadow-sm">
          <div className="mb-5 flex items-center justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#5d7a78]">Trip builder</p>
              <h3 className="mt-1 text-xl font-semibold text-[#24313f]">Create your itinerary</h3>
            </div>
            <button type="button" className="rounded-full border border-[#dfeae8] bg-[#f3f8f7] px-3 py-1.5 text-xs font-semibold text-[#365d5d]">
              Smart assist
            </button>
          </div>

          <div className="mb-6 flex flex-wrap gap-2">
            {templates.map((template) => (
              <button
                key={template.label}
                type="button"
                onClick={() => applyTemplate(template)}
                className="rounded-full border border-[#dfeae7] bg-white px-3 py-1.5 text-xs font-semibold text-[#405d63] shadow-sm transition hover:-translate-y-0.5 hover:border-[#9cc9c2] hover:text-[#173f45]"
              >
                {template.label}
              </button>
            ))}
          </div>

          <form onSubmit={submit} className="grid gap-4 md:grid-cols-2">
            <label className="text-sm font-medium text-[#425663] md:col-span-2">
              Trip name
              <input
                value={form.title}
                onChange={(e) => update("title", e.target.value)}
                placeholder="Bali coastal escape"
                className="mt-2 w-full rounded-2xl border border-[#dfe7e8] bg-[#fbfbfb] px-3 py-2.5 text-[#1f2f34] shadow-sm outline-none transition focus:border-[#79b7af] focus:ring-4 focus:ring-[#dff1ee]"
              />
            </label>

            <label className="text-sm font-medium text-[#425663]">
              Destination
              <select
                value={form.destinationId}
                onChange={(e) => update("destinationId", e.target.value)}
                className="mt-2 w-full rounded-2xl border border-[#dfe7e8] bg-[#fbfbfb] px-3 py-2.5 text-[#1f2f34] shadow-sm outline-none transition focus:border-[#79b7af] focus:ring-4 focus:ring-[#dff1ee]"
              >
                <option value="">Choose a destination</option>
                {destinations.map((destination) => <option key={destination.id} value={destination.id}>{destination.name}{destination.country ? `, ${destination.country}` : ""}</option>)}
              </select>
            </label>

            <label className="text-sm font-medium text-[#425663]">
              Status
              <select
                value={form.status}
                onChange={(e) => update("status", e.target.value)}
                className="mt-2 w-full rounded-2xl border border-[#dfe7e8] bg-[#fbfbfb] px-3 py-2.5 text-[#1f2f34] shadow-sm outline-none transition focus:border-[#79b7af] focus:ring-4 focus:ring-[#dff1ee]"
              >
                <option value="PLANNED">Planned</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </label>

            <label className="text-sm font-medium text-[#425663]">
              Start date
              <input
                type="date"
                value={form.startDate}
                onChange={(e) => update("startDate", e.target.value)}
                className="mt-2 w-full rounded-2xl border border-[#dfe7e8] bg-[#fbfbfb] px-3 py-2.5 text-[#1f2f34] shadow-sm outline-none transition focus:border-[#79b7af] focus:ring-4 focus:ring-[#dff1ee]"
              />
            </label>

            <label className="text-sm font-medium text-[#425663]">
              End date
              <input
                type="date"
                value={form.endDate}
                onChange={(e) => update("endDate", e.target.value)}
                className="mt-2 w-full rounded-2xl border border-[#dfe7e8] bg-[#fbfbfb] px-3 py-2.5 text-[#1f2f34] shadow-sm outline-none transition focus:border-[#79b7af] focus:ring-4 focus:ring-[#dff1ee]"
              />
            </label>

            <label className="text-sm font-medium text-[#425663] md:col-span-2">
              Description
              <textarea
                value={form.description}
                onChange={(e) => update("description", e.target.value)}
                rows={4}
                placeholder="Add your plans, goals, and must-do moments..."
                className="mt-2 w-full rounded-2xl border border-[#dfe7e8] bg-[#fbfbfb] px-3 py-2.5 text-[#1f2f34] shadow-sm outline-none transition focus:border-[#79b7af] focus:ring-4 focus:ring-[#dff1ee]"
              />
            </label>

            {error && (
              <div className="md:col-span-2 rounded-2xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="md:col-span-2 mt-2 flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={submitting}
                className="rounded-2xl bg-gradient-to-r from-[#173f45] via-[#1d535a] to-[#2e7d78] px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-[#dfece8] transition hover:-translate-y-0.5 disabled:opacity-60"
              >
                {submitting ? "Creating trip..." : "Create trip"}
              </button>
              <button type="button" onClick={() => { setError(""); setForm({ title: "", destinationId: "", startDate: "", endDate: "", description: "", status: "PLANNED" }); }} className="rounded-2xl border border-[#dfe7e8] bg-white px-4 py-3 text-sm font-semibold text-[#39585f] transition hover:border-[#b8d5d2] hover:bg-[#f5fbfa]">
                Clear form
              </button>
            </div>
          </form>
        </section>

        <aside className="space-y-4">
          <div className="rounded-[28px] border border-[#e5dfd7] bg-[#f7f8f9] p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.18em] text-[#5d7a78]">Quick checklist</p>
            <div className="mt-4 space-y-3">
              {[
                "Choose destination",
                "Confirm dates",
                "Review budget",
                "Invite travel mates",
              ].map((item, index) => (
                <div key={item} className="flex items-center gap-3 rounded-2xl bg-white p-3 shadow-sm">
                  <div className="flex h-7 w-7 items-center justify-center rounded-full bg-[#e2f3f0] text-xs font-bold text-[#2d4c53]">
                    {index + 1}
                  </div>
                  <span className="text-sm text-[#425663]">{item}</span>
                </div>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </UserShell>
  );
}
