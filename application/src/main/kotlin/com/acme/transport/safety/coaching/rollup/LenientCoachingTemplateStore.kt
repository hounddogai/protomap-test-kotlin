package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient coaching templates by code. */
interface LenientCoachingTemplateStore {
    fun find(code: String): LenientCoachingTemplate?
    fun save(item: LenientCoachingTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCoachingTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCoachingTemplateStore(
    var items: MutableMap<String, LenientCoachingTemplate>,
    var order: MutableList<String>,
) : LenientCoachingTemplateStore {
    override fun find(code: String): LenientCoachingTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCoachingTemplate) {
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

    override fun list(): MutableList<LenientCoachingTemplate> {
        val out = mutableListOf<LenientCoachingTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCoachingTemplateStore(): LenientCoachingTemplateStore {
    return MemoryLenientCoachingTemplateStore(
        mutableMapOf<String, LenientCoachingTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCoachingTemplateStore(
    var inner: LenientCoachingTemplateStore,
    var cache: TtlCache<LenientCoachingTemplate>,
    var hits: Int,
    var misses: Int,
) : LenientCoachingTemplateStore {
    override fun find(code: String): LenientCoachingTemplate? {
        val hit = (cache.get(code) as LenientCoachingTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCoachingTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCoachingTemplate> = inner.list()
}

fun newCachedLenientCoachingTemplateStore(inner: LenientCoachingTemplateStore, ttl: Int, clock: Clock): LenientCoachingTemplateStore {
    return CachedLenientCoachingTemplateStore(
        inner = inner,
        cache = newTtlCache<LenientCoachingTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient coaching templates. Empty fields match all. */
data class LenientCoachingTemplateQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCoachingTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCoachingTemplate>): MutableList<LenientCoachingTemplate> {
        val matched = filter(items) { candidate: LenientCoachingTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCoachingTemplates(store: LenientCoachingTemplateStore, query: LenientCoachingTemplateQuery): MutableList<LenientCoachingTemplate> {
    return query.apply(store.list())
}
