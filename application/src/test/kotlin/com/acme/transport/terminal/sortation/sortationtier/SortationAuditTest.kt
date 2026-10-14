package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationAuditTest {
    @Test
    fun `starts in created`() {
        val item = newSortationAudit("code-763", "name-252")
        assertEquals(SortationAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newSortationAudit("code-763", "name-252")
        try {
            item.moveTo(SortationAuditStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationAuditStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSortationAudit("code-763", "name-252")
        item.status = SortationAuditStatus.RETIRED
        assertFails("move") { item.moveTo(SortationAuditStatus.CREATED) }
    }
}
