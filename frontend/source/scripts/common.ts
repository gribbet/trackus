export function range(start: number, end: number): number[] {
    return new Array(end - start)
        .fill(0)
        .map((_, i) => i + start);
}

export function flatten<T>(x: T[][]): T[] {
    return [].concat.apply([], x);
}

export function randomString(count: number) {
    return Math.random().toString(32).substring(2, 2 + count);
}

export function delay(time: number): Promise<void> {
    return new Promise(_ => setTimeout(_, time))
}