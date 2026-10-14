package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTenderRollupTest {
    @Test
    fun `starts in created`() {
        val item = newManualTenderRollup("code-688", "title-515")
        assertEquals(ManualTenderRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualTenderRollup("code-688", "title-515")
        try {
            item.moveTo(ManualTenderRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTenderRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualTenderRollup("code-688", "title-515")
        item.status = ManualTenderRollupStatus.RETIRED
        assertFails("move") { item.moveTo(ManualTenderRollupStatus.CREATED) }
    }
}
