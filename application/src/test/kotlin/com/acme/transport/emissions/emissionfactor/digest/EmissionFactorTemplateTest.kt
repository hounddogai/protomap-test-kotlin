package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newEmissionFactorTemplate("code-474", "label-622")
        assertEquals(EmissionFactorTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newEmissionFactorTemplate("code-474", "label-622")
        try {
            item.moveTo(EmissionFactorTemplateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorTemplateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEmissionFactorTemplate("code-474", "label-622")
        item.status = EmissionFactorTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(EmissionFactorTemplateStatus.CREATED) }
    }
}
