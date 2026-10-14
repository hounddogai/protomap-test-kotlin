package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryTenderPreference("code-317", "title-58")
        assertEquals(PrimaryTenderPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryTenderPreference("code-317", "title-58")
        try {
            item.moveTo(PrimaryTenderPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryTenderPreference("code-317", "title-58")
        item.status = PrimaryTenderPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryTenderPreferenceStatus.REQUESTED) }
    }
}
