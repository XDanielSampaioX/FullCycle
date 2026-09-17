"use client";

import { useSyncExternalStore } from "react";
import { read, subscribe } from "../session";

const serverSnapshot = () => undefined;

export function useSessao() {
  return useSyncExternalStore(subscribe, read, serverSnapshot);
}
