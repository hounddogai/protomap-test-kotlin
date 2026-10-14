package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTariffDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingTariffDraft("code-332", "name-449")
        assertEquals(RollingTariffDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingTariffDraft("code-332", "name-449")
        try {
            item.moveTo(RollingTariffDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTariffDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingTariffDraft("code-332", "name-449")
        item.status = RollingTariffDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingTariffDraftStatus.PROPOSED) }
    }
}
