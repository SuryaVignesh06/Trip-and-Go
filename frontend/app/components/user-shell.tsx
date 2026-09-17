"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { clearSession } from "../lib/api";

type UserShellProps = { title: string; description: string; activePage: string; children: ReactNode };
const navItems = [
  { name: "Dashboard", href: "/dashboard" }, { name: "My Trips", href: "/trips" }, { name: "Destinations", href: "/destinations" },
  { name: "Join a Trip", href: "/trips/join" },
  { name: "Profile", href: "/profile" },
  { name: "Travel History", href: "/history" }, { name: "Account Settings", href: "/settings" },
];

export function UserShell({ title, description, activePage, children }: UserShellProps) {
  const router = useRouter();
  function signOut() { clearSession(); router.push("/"); }
  return (
    <div className="min-h-screen bg-[#f6f8fc] text-[#172033]">
      <header className="border-b border-slate-200 bg-white"><div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <Link href="/dashboard" className="flex items-center gap-2.5"><div className="grid h-8 w-8 place-items-center rounded-lg bg-[#2452bb] text-sm font-black text-white">T</div><span className="text-lg font-bold tracking-tight">TripNest</span></Link>
        <div className="flex items-center gap-3"><span className="hidden text-sm text-slate-500 sm:inline">Your travel workspace</span><button type="button" onClick={signOut} className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">Sign out</button></div>
      </div></header>
      <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <div className="mb-7 flex flex-col gap-3 border-b border-slate-200 pb-6 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-xs font-bold uppercase tracking-[0.16em] text-[#4266be]">Trip planning</p><h1 className="mt-2 text-3xl font-bold tracking-[-0.04em] text-[#172033]">{title}</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">{description}</p></div></div>
        <div className="grid gap-7 lg:grid-cols-[232px_minmax(0,1fr)]">
          <aside className="lg:sticky lg:top-6 lg:h-fit"><nav aria-label="Account navigation" className="flex gap-2 overflow-x-auto pb-2 lg:block lg:space-y-1 lg:overflow-visible">
            {navItems.map((item) => { const active = item.name === activePage; return <Link key={item.name} href={item.href} className={["shrink-0 rounded-lg px-3 py-2.5 text-sm font-semibold transition lg:flex lg:w-full lg:items-center lg:justify-between", active ? "bg-[#eaf0ff] text-[#214aab]" : "text-slate-600 hover:bg-white hover:text-slate-900"].join(" ")}>{item.name}<span className="hidden text-slate-400 lg:inline" aria-hidden="true">&rarr;</span></Link>; })}
          </nav></aside>
          <main className="min-w-0">{children}</main>
        </div>
      </div>
    </div>
  );
}
