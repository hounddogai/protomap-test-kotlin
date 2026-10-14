package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictLockerTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictLockerTargetEngine(newRegistry())
        val item = newStrictLockerTarget("code-294", "title-993")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
