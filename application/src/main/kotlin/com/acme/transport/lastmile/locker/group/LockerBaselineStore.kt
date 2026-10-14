package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker baselines by code. */
interface LockerBaselineStore {
    fun find(code: String): LockerBaseline?
    fun save(item: LockerBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerBaselineStore(
    var items: MutableMap<String, LockerBaseline>,
    var order: MutableList<String>,
) : LockerBaselineStore {
    override fun find(code: String): LockerBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerBaseline) {
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

    override fun list(): MutableList<LockerBaseline> {
        val out = mutableListOf<LockerBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerBaselineStore(): LockerBaselineStore {
    return MemoryLockerBaselineStore(mutableMapOf<String, LockerBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerBaselineStore(
    var inner: LockerBaselineStore,
    var cache: TtlCache<LockerBaseline>,
    var hits: Int,
    var misses: Int,
) : LockerBaselineStore {
    override fun find(code: String): LockerBaseline? {
        val hit = (cache.get(code) as LockerBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerBaseline> = inner.list()
}

fun newCachedLockerBaselineStore(inner: LockerBaselineStore, ttl: Int, clock: Clock): LockerBaselineStore {
    return CachedLockerBaselineStore(
        inner = inner,
        cache = newTtlCache<LockerBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker baselines. Empty fields match all. */
data class LockerBaselineQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerBaseline>): MutableList<LockerBaseline> {
        val matched = filter(items) { candidate: LockerBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerBaselines(store: LockerBaselineStore, query: LockerBaselineQuery): MutableList<LockerBaseline> {
    return query.apply(store.list())
}
