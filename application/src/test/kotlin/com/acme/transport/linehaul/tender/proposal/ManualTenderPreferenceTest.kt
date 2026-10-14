package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTenderPreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newManualTenderPreference("code-961", "label-433")
        assertEquals(ManualTenderPreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualTenderPreference("code-961", "label-433")
        try {
            item.moveTo(ManualTenderPreferenceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTenderPreferenceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualTenderPreference("code-961", "label-433")
        item.status = ManualTenderPreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(ManualTenderPreferenceStatus.CREATED) }
    }
}
