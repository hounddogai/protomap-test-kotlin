package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDefectBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDefectBundleEngine(newRegistry())
        val item = newPrimaryDefectBundle("code-210", "title-70")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
