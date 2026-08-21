public record Move(int fromRow, int fromCol, int toRow, int toCol, char promotion) {
    // promotion is 'Q', 'R', 'B', 'N', or '\0' if none
}