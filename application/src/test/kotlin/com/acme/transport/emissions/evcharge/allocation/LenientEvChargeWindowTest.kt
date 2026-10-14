package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientEvChargeWindowTest {
    @Test
    fun `starts in created`() {
        val item = newLenientEvChargeWindow("code-848", "name-102")
        assertEquals(LenientEvChargeWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientEvChargeWindow("code-848", "name-102")
        try {
            item.moveTo(LenientEvChargeWindowStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientEvChargeWindowStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientEvChargeWindow("code-848", "name-102")
        item.status = LenientEvChargeWindowStatus.RETIRED
        assertFails("move") { item.moveTo(LenientEvChargeWindowStatus.CREATED) }
    }
}
