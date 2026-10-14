package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalInspectionDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalInspectionDigestEngine(newRegistry())
        val item = newSeasonalInspectionDigest("code-78", "label-684")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
