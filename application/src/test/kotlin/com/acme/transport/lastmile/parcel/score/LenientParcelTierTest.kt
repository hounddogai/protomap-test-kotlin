package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientParcelTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientParcelTier("code-991", "title-278")
        assertEquals(LenientParcelTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLenientParcelTier("code-991", "title-278")
        try {
            item.moveTo(LenientParcelTierStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientParcelTierStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientParcelTier("code-991", "title-278")
        item.status = LenientParcelTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientParcelTierStatus.PROPOSED) }
    }
}
