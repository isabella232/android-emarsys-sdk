package com.emarsys.core.experimental;

import com.emarsys.core.api.experimental.FlipperFeature;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExperimentalFeaturesTest {

    private FlipperFeature feature1;
    private FlipperFeature feature2;
    private FlipperFeature feature3;
    private List<FlipperFeature> features;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        ExperimentalFeatures.reset();

        feature1 = mock(FlipperFeature.class);
        when(feature1.getName()).thenReturn("feature1");

        feature2 = mock(FlipperFeature.class);
        when(feature2.getName()).thenReturn("feature2");

        feature3 = mock(FlipperFeature.class);
        when(feature3.getName()).thenReturn("feature3");

        features = Arrays.asList(feature1, feature2, feature3);
    }

    @After
    public void tearDown() {
        ExperimentalFeatures.reset();
    }

    @Test
    public void testIsFeatureEnabled_shouldDefaultToBeingTurnedOff() {
        for (FlipperFeature feature : features) {
            assertFalse(ExperimentalFeatures.isFeatureEnabled(feature));
        }
    }

    @Test
    public void testIsFeatureEnabled_shouldReturnTrue_whenFeatureIsTurnedOn() {
        ExperimentalFeatures.enableFeature(feature1);
        assertTrue(ExperimentalFeatures.isFeatureEnabled(feature1));
    }

    @Test
    public void testEnableFeature_shouldAppendFeaturesToTheEnabledFeatureSet() {
        assertEquals(0, ExperimentalFeatures.enabledFeatures.size());
        ExperimentalFeatures.enableFeature(feature1);
        ExperimentalFeatures.enableFeature(feature2);
        assertEquals(2, ExperimentalFeatures.enabledFeatures.size());
    }

    @Test
    public void testEnableFeature_shouldRemoveAllFeaturesFromTheEnabledFeatureSet() {
        ExperimentalFeatures.enableFeature(feature1);
        ExperimentalFeatures.enableFeature(feature2);
        assertEquals(2, ExperimentalFeatures.enabledFeatures.size());
        ExperimentalFeatures.reset();
        assertEquals(0, ExperimentalFeatures.enabledFeatures.size());
    }

}