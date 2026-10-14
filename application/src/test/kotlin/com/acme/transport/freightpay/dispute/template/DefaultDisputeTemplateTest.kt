package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDisputeTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultDisputeTemplate("code-316", "title-336")
        assertEquals(DefaultDisputeTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newDefaultDisputeTemplate("code-316", "title-336")
        try {
            item.moveTo(DefaultDisputeTemplateStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDisputeTemplateStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultDisputeTemplate("code-316", "title-336")
        item.status = DefaultDisputeTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultDisputeTemplateStatus.DRAFT) }
    }
}
