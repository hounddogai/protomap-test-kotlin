package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCutoffOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalCutoffOverride("code-828", "label-496")
        assertEquals(RegionalCutoffOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalCutoffOverride("code-828", "label-496")
        try {
            item.moveTo(RegionalCutoffOverrideStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCutoffOverrideStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalCutoffOverride("code-828", "label-496")
        item.status = RegionalCutoffOverrideStatus.FAILED
        assertFails("move") { item.moveTo(RegionalCutoffOverrideStatus.NEW) }
    }
}
