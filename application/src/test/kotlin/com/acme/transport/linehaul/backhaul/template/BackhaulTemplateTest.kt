package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newBackhaulTemplate("code-306", "name-224")
        assertEquals(BackhaulTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBackhaulTemplate("code-306", "name-224")
        try {
            item.moveTo(BackhaulTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBackhaulTemplate("code-306", "name-224")
        item.status = BackhaulTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(BackhaulTemplateStatus.DRAFT) }
    }
}
