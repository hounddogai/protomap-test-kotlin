package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDropDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDropDigest("code-345", "title-812")
        val view = toBatchedDropDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDropDigestViews(mutableListOf<BatchedDropDigest>(item, item)).size, "views")
    }
}
