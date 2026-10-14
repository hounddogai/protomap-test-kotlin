package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul targets by code. */
interface BackhaulTargetStore {
    fun find(code: String): BackhaulTarget?
    fun save(item: BackhaulTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulTargetStore(
    var items: MutableMap<String, BackhaulTarget>,
    var order: MutableList<String>,
) : BackhaulTargetStore {
    override fun find(code: String): BackhaulTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulTarget) {
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

    override fun list(): MutableList<BackhaulTarget> {
        val out = mutableListOf<BackhaulTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulTargetStore(): BackhaulTargetStore {
    return MemoryBackhaulTargetStore(mutableMapOf<String, BackhaulTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulTargetStore(
    var inner: BackhaulTargetStore,
    var cache: TtlCache<BackhaulTarget>,
    var hits: Int,
    var misses: Int,
) : BackhaulTargetStore {
    override fun find(code: String): BackhaulTarget? {
        val hit = (cache.get(code) as BackhaulTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulTarget> = inner.list()
}

fun newCachedBackhaulTargetStore(inner: BackhaulTargetStore, ttl: Int, clock: Clock): BackhaulTargetStore {
    return CachedBackhaulTargetStore(
        inner = inner,
        cache = newTtlCache<BackhaulTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul targets. Empty fields match all. */
data class BackhaulTargetQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulTarget>): MutableList<BackhaulTarget> {
        val matched = filter(items) { candidate: BackhaulTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulTargets(store: BackhaulTargetStore, query: BackhaulTargetQuery): MutableList<BackhaulTarget> {
    return query.apply(store.list())
}
