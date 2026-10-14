package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTenderRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryTenderRollup("code-868", "name-811")
        assertEquals(PrimaryTenderRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryTenderRollup("code-868", "name-811")
        try {
            item.moveTo(PrimaryTenderRollupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTenderRollupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryTenderRollup("code-868", "name-811")
        item.status = PrimaryTenderRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryTenderRollupStatus.REQUESTED) }
    }
}
