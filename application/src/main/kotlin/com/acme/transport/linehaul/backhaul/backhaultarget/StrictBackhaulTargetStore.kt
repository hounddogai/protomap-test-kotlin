package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict backhaul targets by code. */
interface StrictBackhaulTargetStore {
    fun find(code: String): StrictBackhaulTarget?
    fun save(item: StrictBackhaulTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBackhaulTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBackhaulTargetStore(
    var items: MutableMap<String, StrictBackhaulTarget>,
    var order: MutableList<String>,
) : StrictBackhaulTargetStore {
    override fun find(code: String): StrictBackhaulTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBackhaulTarget) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<StrictBackhaulTarget> {
        val out = mutableListOf<StrictBackhaulTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBackhaulTargetStore(): StrictBackhaulTargetStore {
    return MemoryStrictBackhaulTargetStore(
        mutableMapOf<String, StrictBackhaulTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBackhaulTargetStore(
    var inner: StrictBackhaulTargetStore,
    var cache: TtlCache<StrictBackhaulTarget>,
    var hits: Int,
    var misses: Int,
) : StrictBackhaulTargetStore {
    override fun find(code: String): StrictBackhaulTarget? {
        val hit = (cache.get(code) as StrictBackhaulTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBackhaulTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBackhaulTarget> = inner.list()
}

fun newCachedStrictBackhaulTargetStore(inner: StrictBackhaulTargetStore, ttl: Int, clock: Clock): StrictBackhaulTargetStore {
    return CachedStrictBackhaulTargetStore(
        inner = inner,
        cache = newTtlCache<StrictBackhaulTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict backhaul targets. Empty fields match all. */
data class StrictBackhaulTargetQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBackhaulTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBackhaulTarget>): MutableList<StrictBackhaulTarget> {
        val matched = filter(items) { candidate: StrictBackhaulTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBackhaulTargets(store: StrictBackhaulTargetStore, query: StrictBackhaulTargetQuery): MutableList<StrictBackhaulTarget> {
    return query.apply(store.list())
}
