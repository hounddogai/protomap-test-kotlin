package com.acme.transport.emissions.emissionfactor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEmissionFactorMappingTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveEmissionFactorMapping("code-795", "name-784")
        assertEquals(AdaptiveEmissionFactorMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAdaptiveEmissionFactorMapping("code-795", "name-784")
        try {
            item.moveTo(AdaptiveEmissionFactorMappingStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEmissionFactorMappingStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveEmissionFactorMapping("code-795", "name-784")
        item.status = AdaptiveEmissionFactorMappingStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveEmissionFactorMappingStatus.CREATED) }
    }
}
