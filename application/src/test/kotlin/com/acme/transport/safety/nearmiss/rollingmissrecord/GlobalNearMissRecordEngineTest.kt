package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalNearMissRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalNearMissRecordEngine(newRegistry())
        val item = newGlobalNearMissRecord("code-877", "name-820")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
