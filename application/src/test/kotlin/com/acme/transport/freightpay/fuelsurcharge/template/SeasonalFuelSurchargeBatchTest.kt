package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFuelSurchargeBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalFuelSurchargeBatch("code-933", "name-84")
        assertEquals(SeasonalFuelSurchargeBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalFuelSurchargeBatch("code-933", "name-84")
        try {
            item.moveTo(SeasonalFuelSurchargeBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFuelSurchargeBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalFuelSurchargeBatch("code-933", "name-84")
        item.status = SeasonalFuelSurchargeBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalFuelSurchargeBatchStatus.REQUESTED) }
    }
}
