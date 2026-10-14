package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDefectProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDefectProfileEngine(newRegistry())
        val item = newScheduledDefectProfile("code-128", "name-384")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
