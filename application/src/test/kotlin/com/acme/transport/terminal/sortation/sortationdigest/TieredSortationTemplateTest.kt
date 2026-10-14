package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredSortationTemplate("code-439", "name-728")
        assertEquals(TieredSortationTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTieredSortationTemplate("code-439", "name-728")
        try {
            item.moveTo(TieredSortationTemplateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationTemplateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredSortationTemplate("code-439", "name-728")
        item.status = TieredSortationTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(TieredSortationTemplateStatus.DRAFT) }
    }
}
