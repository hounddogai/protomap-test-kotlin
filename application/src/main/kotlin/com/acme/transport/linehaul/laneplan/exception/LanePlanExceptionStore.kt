package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan exceptions by code. */
interface LanePlanExceptionStore {
    fun find(code: String): LanePlanException?
    fun save(item: LanePlanException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanExceptionStore(
    var items: MutableMap<String, LanePlanException>,
    var order: MutableList<String>,
) : LanePlanExceptionStore {
    override fun find(code: String): LanePlanException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanException) {
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

    override fun list(): MutableList<LanePlanException> {
        val out = mutableListOf<LanePlanException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanExceptionStore(): LanePlanExceptionStore {
    return MemoryLanePlanExceptionStore(mutableMapOf<String, LanePlanException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanExceptionStore(
    var inner: LanePlanExceptionStore,
    var cache: TtlCache<LanePlanException>,
    var hits: Int,
    var misses: Int,
) : LanePlanExceptionStore {
    override fun find(code: String): LanePlanException? {
        val hit = (cache.get(code) as LanePlanException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanException> = inner.list()
}

fun newCachedLanePlanExceptionStore(inner: LanePlanExceptionStore, ttl: Int, clock: Clock): LanePlanExceptionStore {
    return CachedLanePlanExceptionStore(
        inner = inner,
        cache = newTtlCache<LanePlanException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan exceptions. Empty fields match all. */
data class LanePlanExceptionQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanException): Boolean {
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

    fun apply(items: MutableList<LanePlanException>): MutableList<LanePlanException> {
        val matched = filter(items) { candidate: LanePlanException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanExceptions(store: LanePlanExceptionStore, query: LanePlanExceptionQuery): MutableList<LanePlanException> {
    return query.apply(store.list())
}
