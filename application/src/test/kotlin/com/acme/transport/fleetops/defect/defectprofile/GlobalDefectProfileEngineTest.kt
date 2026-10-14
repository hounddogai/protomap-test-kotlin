package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDefectProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDefectProfileEngine(newRegistry())
        val item = newGlobalDefectProfile("code-310", "name-460")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
