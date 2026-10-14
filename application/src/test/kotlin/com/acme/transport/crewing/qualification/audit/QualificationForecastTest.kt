package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationForecastTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationForecast("code-733", "name-947")
        assertEquals(QualificationForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newQualificationForecast("code-733", "name-947")
        try {
            item.moveTo(QualificationForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationForecast("code-733", "name-947")
        item.status = QualificationForecastStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationForecastStatus.CREATED) }
    }
}
