package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliveryPreference("code-490", "name-279")
        assertEquals(RedeliveryPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRedeliveryPreference("code-490", "name-279")
        try {
            item.moveTo(RedeliveryPreferenceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryPreferenceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliveryPreference("code-490", "name-279")
        item.status = RedeliveryPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliveryPreferenceStatus.IDLE) }
    }
}
