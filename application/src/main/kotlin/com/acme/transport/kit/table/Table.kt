package com.acme.transport.kit.table

// Plain-text and CSV tables for reports.

data class Table(
    var title: String,
    var headers: MutableList<String>,
    var rows: MutableList<MutableList<String>>,
) {
    fun addRow(cells: MutableList<String>) {
        if (cells.size != headers.size) {
            error("row has ${cells.size} cells, table $title has ${headers.size} columns")
        }
        rows.add(cells)
    }

    fun widths(): MutableList<Int> {
        val widths = mutableListOf<Int>()
        for (header in headers) {
            widths.add(header.length)
        }
        for (row in rows) {
            for (i in 0 until row.size) {
                if (row[i].length > widths[i]) {
                    widths[i] = row[i].length
                }
            }
        }
        return widths
    }

    fun render(): String {
        val widths = widths()
        val lines = mutableListOf<String>(title)
        val head = mutableListOf<String>()
        for (i in 0 until headers.size) {
            head.add(headers[i].padEnd(widths[i], ' '))
        }
        lines.add(head.joinToString(" | "))
        for (row in rows) {
            val padded = mutableListOf<String>()
            for (i in 0 until row.size) {
                padded.add(row[i].padEnd(widths[i], ' '))
            }
            lines.add(padded.joinToString(" | "))
        }
        return lines.joinToString("\n")
    }

    fun csv(): String {
        val lines = mutableListOf<String>(headers.joinToString(","))
        for (row in rows) {
            lines.add(row.joinToString(","))
        }
        return lines.joinToString("\n")
    }
}

fun newTable(title: String, headers: MutableList<String>): Table {
    return Table(title, headers, mutableListOf<MutableList<String>>())
}
