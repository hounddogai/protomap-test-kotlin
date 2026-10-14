package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDeclarationBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledDeclarationBaseline("code-336", "name-975")
        assertEquals(ScheduledDeclarationBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledDeclarationBaseline("code-336", "name-975")
        try {
            item.moveTo(ScheduledDeclarationBaselineStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDeclarationBaselineStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledDeclarationBaseline("code-336", "name-975")
        item.status = ScheduledDeclarationBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledDeclarationBaselineStatus.CREATED) }
    }
}
