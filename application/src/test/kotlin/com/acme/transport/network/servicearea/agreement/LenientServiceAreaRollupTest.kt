package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaRollupTest {
    @Test
    fun `starts in new`() {
        val item = newLenientServiceAreaRollup("code-840", "title-432")
        assertEquals(LenientServiceAreaRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientServiceAreaRollup("code-840", "title-432")
        try {
            item.moveTo(LenientServiceAreaRollupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaRollupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientServiceAreaRollup("code-840", "title-432")
        item.status = LenientServiceAreaRollupStatus.FAILED
        assertFails("move") { item.moveTo(LenientServiceAreaRollupStatus.NEW) }
    }
}
