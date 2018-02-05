import Position from "./Position";

export default class PositionService {

    private map: {
        [id: string]: Position[];
    } = {};

    add(position: Position) {
        let index = this.findIndex(
            position.user,
            position.timestamp);

        const positions = this.positions(position.user);

        if (positions.length > 0) {
            const existing = positions[index];
            if (existing.timestamp === position.timestamp)
                return;
            if (existing.latitude === position.latitude
                && existing.longitude === position.longitude)
                return;
        }

        positions.splice(
            index,
            0,
            position);
    }

    slice(user: string, start: number, end: number): Position[] {
        let startIndex = this.findIndex(user, end);
        let endIndex = this.findIndex(user, start);
        const positions = this.positions(user);

        if (startIndex === -1) startIndex = positions.length - 1;
        if (startIndex > 0) startIndex = startIndex - 1;
        if (endIndex === -1) endIndex = positions.length;

        return positions.slice(startIndex, endIndex);
    }

    users(): string[] {
        return Object.keys(this.map);
    }

    current(user: string): Position {
        return this.positions(user)[0];
    }

    private positions(user: string): Position[] {
        const id = user.toString();
        return this.map[id] =
            this.map[id] || [];
    }

    private findIndex(user: string, timestamp: number): number {
        const positions = this.positions(user);
        let index = positions
            .findIndex(_ => _.timestamp <= timestamp);
        if (index === -1)
            index = positions.length > 0 ? positions.length - 1 : 0;
        return index;
    }
}