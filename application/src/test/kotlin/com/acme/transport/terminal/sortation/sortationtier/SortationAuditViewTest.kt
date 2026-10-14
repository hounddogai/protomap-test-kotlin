package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationAudit("code-763", "name-252")
        val view = toSortationAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationAuditViews(mutableListOf<SortationAudit>(item, item)).size, "views")
    }
}
