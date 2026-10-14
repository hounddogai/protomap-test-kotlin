package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBackhaulThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalBackhaulThreshold("code-93", "name-89")
        assertEquals(GlobalBackhaulThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalBackhaulThreshold("code-93", "name-89")
        try {
            item.moveTo(GlobalBackhaulThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBackhaulThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalBackhaulThreshold("code-93", "name-89")
        item.status = GlobalBackhaulThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalBackhaulThresholdStatus.PROPOSED) }
    }
}
