package com.acme.transport.emissions.idling.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingQuota("code-165", "label-110")
        assertEquals(IdlingQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newIdlingQuota("code-165", "label-110")
        try {
            item.moveTo(IdlingQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingQuota("code-165", "label-110")
        item.status = IdlingQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingQuotaStatus.DRAFT) }
    }
}
