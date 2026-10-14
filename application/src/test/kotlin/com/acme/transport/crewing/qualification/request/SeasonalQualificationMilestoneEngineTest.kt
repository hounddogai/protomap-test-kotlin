package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalQualificationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalQualificationMilestoneEngine(newRegistry())
        val item = newSeasonalQualificationMilestone("code-705", "label-971")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
