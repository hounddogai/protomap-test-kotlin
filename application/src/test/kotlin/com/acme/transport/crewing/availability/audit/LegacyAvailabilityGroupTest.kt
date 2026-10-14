package com.acme.transport.crewing.availability.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAvailabilityGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyAvailabilityGroup("code-308", "label-334")
        assertEquals(LegacyAvailabilityGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLegacyAvailabilityGroup("code-308", "label-334")
        try {
            item.moveTo(LegacyAvailabilityGroupStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAvailabilityGroupStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyAvailabilityGroup("code-308", "label-334")
        item.status = LegacyAvailabilityGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyAvailabilityGroupStatus.PROPOSED) }
    }
}
