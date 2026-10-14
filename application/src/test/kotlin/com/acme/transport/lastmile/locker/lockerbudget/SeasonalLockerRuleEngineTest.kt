package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalLockerRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalLockerRuleEngine()
        val item = newSeasonalLockerRule("code-617", "title-813")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
