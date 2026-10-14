package com.acme.transport.linehaul.tender.weightedtenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTenderRollupTest {
    @Test
    fun `starts in created`() {
        val item = newRollingTenderRollup("code-860", "title-414")
        assertEquals(RollingTenderRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingTenderRollup("code-860", "title-414")
        try {
            item.moveTo(RollingTenderRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTenderRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingTenderRollup("code-860", "title-414")
        item.status = RollingTenderRollupStatus.RETIRED
        assertFails("move") { item.moveTo(RollingTenderRollupStatus.CREATED) }
    }
}
