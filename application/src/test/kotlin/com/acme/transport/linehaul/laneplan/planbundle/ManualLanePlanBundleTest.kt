package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualLanePlanBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newManualLanePlanBundle("code-699", "label-931")
        assertEquals(ManualLanePlanBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualLanePlanBundle("code-699", "label-931")
        try {
            item.moveTo(ManualLanePlanBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualLanePlanBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualLanePlanBundle("code-699", "label-931")
        item.status = ManualLanePlanBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualLanePlanBundleStatus.REQUESTED) }
    }
}
