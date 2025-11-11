import { iso6393 } from "iso-639-3";

export const QRCODE_PLACEHOLDER = "{{credentialSubject/qrCodeImage}}"


export const BENEFITS_PLACEHOLDER_REGEX_PATTERN = "\\{\\{credentialSubject/benefitsLine\\d+\\}\\}"
export const BENEFITS_FIELD_NAME = "benefits"

export const DEFAULT_ENG = "eng";

export function normalizeLanguageCode(lang: string): string {
  if (!lang) return DEFAULT_ENG;

  const code = lang.toLowerCase();

  if (code.length === 3) {
    const valid3 = (iso6393 as any).find((entry: any) => entry.iso6393 === code);
    if (valid3) return code;
  }

  if (code.length === 2) {
    const valid2 = (iso6393 as any).find((entry: any) => entry.iso6391 === code);
    if (valid2) return valid2.iso6393;
  }

  return DEFAULT_ENG;
}

export function getLanguageCodes(lang: string): string[] {
  const normalized = normalizeLanguageCode(lang);
  const entry = (iso6393 as any).find((e: any) => e.iso6393 === normalized);

  const aliases = new Set<string>();
  if (!entry) return [normalized];
  if (entry?.iso6391) aliases.add(entry.iso6391);
  aliases.add(entry.iso6393);

  return Array.from(aliases);
}
