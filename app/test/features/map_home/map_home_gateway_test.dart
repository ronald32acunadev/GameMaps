import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gamemaps/features/map_home/method_channel_map_home_gateway.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  const channel = MethodChannel('gamemaps/map_home');
  const gateway = MethodChannelMapHomeGateway();
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
  tearDown(() => messenger.setMockMethodCallHandler(channel, null));
  test(
    'missing native integration returns explicit unavailable states',
    () async {
      expect(
        (await gateway.locate(requestPermission: true)).status.name,
        'unavailable',
      );
      expect((await gateway.search()).category, 'unavailable');
    },
  );
  test(
    'selected response must contain valid identity and coordinates',
    () async {
      for (final payload in [
        {'status': 'selected'},
        {
          'status': 'selected',
          'placeId': 'p',
          'name': 'Place',
          'latitude': 91,
          'longitude': 0,
        },
        {
          'status': 'selected',
          'placeId': '',
          'name': 'Place',
          'latitude': 0,
          'longitude': 0,
        },
        {'status': 'unknown'},
      ]) {
        messenger.setMockMethodCallHandler(channel, (_) async => payload);
        final result = await gateway.search();
        expect(result.status.name, 'failed');
        expect(result.destination, isNull);
      }
    },
  );
  test('valid selected response and approximate location are parsed', () async {
    messenger.setMockMethodCallHandler(channel, (call) async {
      if (call.method == 'locate') {
        expect(call.arguments, {'requestPermission': true});
        return {
          'status': 'available',
          'latitude': 9.9,
          'longitude': -84.1,
          'approximate': true,
        };
      }
      expect(call.method, 'search');
      return {
        'status': 'selected',
        'placeId': 'p',
        'name': 'Place',
        'latitude': 9.9,
        'longitude': -84.1,
      };
    });
    expect((await gateway.locate(requestPermission: true)).approximate, isTrue);
    expect((await gateway.search()).destination?.placeId, 'p');
  });
  test('native error details are not exposed to UI', () async {
    messenger.setMockMethodCallHandler(
      channel,
      (_) async => throw PlatformException(
        code: 'SDK_FAILURE',
        message: 'sensitive native details',
      ),
    );
    final result = await gateway.search();
    expect(result.category, 'unavailable');
    expect(result.destination, isNull);
  });
}
