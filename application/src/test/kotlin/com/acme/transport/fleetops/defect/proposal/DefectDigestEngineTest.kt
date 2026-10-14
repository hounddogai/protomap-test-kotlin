package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectDigestEngine(newRegistry())
        val item = newDefectDigest("code-983", "label-797")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
