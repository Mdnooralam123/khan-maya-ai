/** Android-safe character registry. The original archive does not contain the licensed Evelyn PMX module. */
import type { CharacterConfig } from './types';
export interface CharacterListing { id:string; displayName:string; source:'built-in'|'user'; restrictions?:string[]; summary?:{supported:number;partial:number;unsupported:number;testsPassed:number;testsFailed:number}; }
export const PUBLIC_BUILD = true;
export const DEFAULT_CHARACTER_ID = 'android-avatar';
export const NO_CHARACTER = 'NO_CHARACTER';
export const CHARACTERS: Record<string, CharacterConfig> = {};
export function listCharacters(){ return [{id:DEFAULT_CHARACTER_ID,displayName:'MYRAA'}]; }
export function isBuiltInCharacter(id: string): boolean { return id === DEFAULT_CHARACTER_ID || id in CHARACTERS; }
export async function listAllCharacters():Promise<CharacterListing[]> { return [{id:DEFAULT_CHARACTER_ID,displayName:'MYRAA',source:'built-in'}]; }
export async function resolveCharacter(_id?:string):Promise<{config:CharacterConfig}> { throw new Error(NO_CHARACTER); }
