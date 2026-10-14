package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalQualificationVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalQualificationVariance("code-33", "label-236")
        assertEquals(GlobalQualificationVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalQualificationVariance("code-33", "label-236")
        try {
            item.moveTo(GlobalQualificationVarianceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalQualificationVarianceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalQualificationVariance("code-33", "label-236")
        item.status = GlobalQualificationVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalQualificationVarianceStatus.IDLE) }
    }
}
