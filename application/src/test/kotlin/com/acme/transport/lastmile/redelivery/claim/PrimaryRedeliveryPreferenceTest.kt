package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRedeliveryPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryRedeliveryPreference("code-846", "name-810")
        assertEquals(PrimaryRedeliveryPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPrimaryRedeliveryPreference("code-846", "name-810")
        try {
            item.moveTo(PrimaryRedeliveryPreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRedeliveryPreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryRedeliveryPreference("code-846", "name-810")
        item.status = PrimaryRedeliveryPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryRedeliveryPreferenceStatus.NEW) }
    }
}
